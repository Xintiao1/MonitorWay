package cn.mw.monitor.user.advcontrol;

import cn.mw.monitor.api.exception.TimeParseException;
import lombok.Data;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class TimeStrategy implements UserControlStra<TimeMessage> {
    private String timeRule;
    private ControlType controlType = ControlType.TIME;

    public TimeStrategy(String timeRule) {
        this.timeRule = timeRule;
    }

    @Override
    public boolean check(TimeMessage timeMessage) {
        LocalDateTime nowDateTime = LocalDateTime.parse(timeMessage.getTime(),
                DateTimeFormatter.ofPattern("yyyyMMdd HH:mm"));
        LocalDate nowDate = nowDateTime.toLocalDate();
        LocalTime nowTime = nowDateTime.toLocalTime();

        //访问标识，默认为false，限制访问
        boolean flag = false;

        if (!timeRule.equals("")) {
            try {
                String[] allowTimeRanges = timeRule.split(",");
                for (String timeField : allowTimeRanges) {
                    Map<String, Object> timeAll = getDateTimeFromString(timeField);
                    List<String> timeSigle = (List<String>) timeAll.get("timeSigle");
                    String[] timePeriod = (String[]) timeAll.get("timePeriod");
                    if (timeField.startsWith("D")) {
                        LocalDate startDate = LocalDate.parse(timePeriod[0],
                                DateTimeFormatter.ofPattern("yyyyMMdd"));
                        LocalDate endDate = LocalDate.parse(timePeriod[1],
                                DateTimeFormatter.ofPattern("yyyyMMdd"));

                        if (timeSigle != null && timeSigle.size() > 0) {
                            boolean timeSigleContain = false;
                            for (String timeSig : timeSigle) {
                                String timeContain = LocalDate.parse(timeSig,
                                        DateTimeFormatter.ofPattern("yyyyMMdd")).toString();
                                if (timeContain.equals(nowDate.toString())) {
                                    timeSigleContain = true;
                                    break;
                                }
                            }
                            if ((nowDate.isAfter(startDate) && nowDate.isBefore(endDate)) || timeSigleContain) {
                                flag = true;
                                break;
                            }else {
                                flag = false;
                            }
                        } else {
                            if (!(nowDate.isAfter(startDate) && nowDate.isBefore(endDate))) {
                                flag = false;
                            }
                            flag = true;
                            break;
                        }
                    }

                    if (timeField.startsWith("T")) {
                        LocalTime startTime = LocalTime.parse(timePeriod[0],
                                DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime endTime = LocalTime.parse(timePeriod[1],
                                DateTimeFormatter.ofPattern("HH:mm"));

                        if (timeSigle != null && timeSigle.size() > 0) {
                            boolean timeSigleContain = timeSigle.contains(nowTime.toString());
                            if ((nowTime.isAfter(startTime) && nowTime.isBefore(endTime)) || timeSigleContain) {
                                flag = true;
                                break;
                            }else {
                                flag = false;
                            }
                        } else {
                            if ((nowTime.isAfter(startTime) && nowTime.isBefore(endTime))) {
                                flag = true;
                                break;
                            }
                        }
                    }
                }
            }catch (Exception e) {
                throw new TimeParseException();
            }
        }else {
            flag = true;
        }
        return flag;
    }

    /*
     * 字符串中提取数字 - ：
     * timeSigle  时间区间的单个时间点
     * timePeriod  时间区间的一个时间段
     * */
    public Map<String, Object> getDateTimeFromString(String timeField) {

        Pattern p = Pattern.compile("[^0-9-:/]");
        Matcher matcher = p.matcher(timeField);
        String timeFromString = matcher.replaceAll("").trim();
        Map<String, Object> timeAll = new HashMap<>();
        if (timeFromString.contains("/")) {
            String[] split = timeFromString.split("/");
            List<String> timeSigle = new ArrayList<>();
            String[] timePeriod = {};
            for (String s : split) {
                if (!s.contains("-")) {
                    timeSigle.add(s);
                    continue;
                }else {
                    timePeriod = s.split("-");
                }
                timeAll.put("timeSigle", timeSigle);
                timeAll.put("timePeriod", timePeriod);
            }
        } else {
            List<String> timeSigle = new ArrayList<>();
            if (!timeFromString.contains("-")) {
                timeSigle.add(timeFromString);
            }
            String[] timePeriod = timeFromString.split("-");
            timeAll.put("timeSigle", timeSigle);
            timeAll.put("timePeriod", timePeriod);
        }
        return timeAll;
    }
}
