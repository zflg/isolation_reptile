package isolation.reptile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @description:
 * @author: Yanqi
 * @date: 2023/12/1
 **/
public class IsolationClient {

  private final RestTemplate restTemplate = new RestTemplate();

  private final static String URL = "http://isolation.spsipcdc.com:9090/WaterInfo/getWaterInfoData";
  private final static String ORG_CODE = "10000027";
  private final static String TYPE = "1";
  private final static long INTERVAL_MINUTES = 15L;

  private final static String UDP_IP = "117.177.179.143";
  private final static int UDP_PORT = 11011;

  public List<WaterInfoVo> getWaterList() {

    // 创建表单数据
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("orgCode", ORG_CODE);
    formData.add("type", TYPE);
    formData.add("date", date);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

    // 发送POST请求
    System.out.println("Request body: " + formData);
    ResponseEntity<String> response = restTemplate.postForEntity(URL, requestEntity, String.class);

    // 处理响应
//    System.out.println("Response status code: " + response.getStatusCode());
//    System.out.println("Response body: " + response.getBody());

    WaterInfoList bean = JsonUtils.toBean(response.getBody(), WaterInfoList.class);
    if (bean == null) {
      System.out.printf("Failed to parse response body: %s%n", response.getBody());
      return List.of();
    }
    return bean.getData();
  }

  /**
   * @param str
   * @return
   */
  private static String addChecksum(String str) {
    byte[] arrByte = str.getBytes();
    byte xx = 0;
    for (byte temp : arrByte) {
      xx += temp;
    }
    byte r = (byte) (~xx + 1);
    byte hi4 = (byte) ((r & 0xf0) >> 4);
    byte lo4 = (byte) (r & 0x0f);
    byte check1 = (byte) (hi4 + 0x61);
    byte check2 = (byte) (lo4 + 0x61);
    byte[] temp2 = {check1, check2};
    String checksum = new String(temp2);
    return str + checksum + "NN";
  }

  private static String buildMsg(Integer workPower) {
    //check null.
    if (workPower == null) {
      System.out.println("workPower is null");
      return null;
    }
    // format date
    SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
    String date = sdf.format(new Date());
    // format workPower
    String workPowerStr = String.valueOf(workPower * 100);
    // format body
    String body = String.format("ST 0817202201 TT %s NS01 %s BV 138 SI1 15 DC 19 ", date, workPowerStr);
    // add checksum
    return addChecksum(body);
  }

  private void send(String msg) {
    try (DatagramSocket socket = new DatagramSocket()) {
      // 创建UDP套接字

      // 准备要发送的数据
      byte[] sendData = msg.getBytes();

      // 创建数据报，指定服务器地址和端口
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(UDP_IP), UDP_PORT);

      // 发送数据报
      socket.send(sendPacket);

      System.out.println("Data sent to server successfully.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class WaterInfoList {
    @Default
    private List<WaterInfoVo> data = new ArrayList<>();
  }

  public static void main(String[] args) {
    // 创建实例对象
    IsolationClient isolationClient = new IsolationClient();

    // 定义间隔时间为5分钟
    long interval = INTERVAL_MINUTES * 60 * 1000; // 5分钟

    // 创建一个 TimerTask 匿名内部类
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        List<WaterInfoVo> waterList = isolationClient.getWaterList();
        if (CollectionUtils.isEmpty(waterList)) {
          return;
        }
        WaterInfoVo waterInfo = waterList.get(0);
        System.out.println("WaterInfoVo: " + waterInfo);
        String msg = buildMsg(waterInfo.getWorkPower());
        System.out.println("Msg: " + msg);
        if (msg != null) {
          isolationClient.send(msg);
        }
      }
    };

    // 每五分钟执行一次任务
    new Timer().schedule(task, 0, interval);
  }

}
