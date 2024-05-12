package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/9/1 14:53
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private MessageCount todayMessage;
    private MessageCount sumMessage;


}

