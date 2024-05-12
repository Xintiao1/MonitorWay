package cn.mw.monitor.user.advcontrol;

public interface UserControlStra<T> {
    boolean check(T checkobj);
    ControlType getControlType();
}
