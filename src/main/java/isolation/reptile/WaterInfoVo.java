package isolation.reptile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: Yanqi
 * @date: 2023/12/1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaterInfoVo {

  @JsonProperty("ID_")
  private Long externalId;
  @JsonProperty("RECORDQUARTER")
  private String time;
  @JsonProperty("RECORDDATE")
  private String date;
  @JsonProperty("WORKPOWER")
  private Integer workPower;

}
