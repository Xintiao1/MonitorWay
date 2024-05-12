package cn.mw.monitor.server.serverdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author syt
 * @Date 2020/9/29 17:14
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareListDto {
    private String Caption;
    private String Vendor;
    private String Version;

    private boolean notShowVendor;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoftwareListDto that = (SoftwareListDto) o;
        return Objects.equals(Caption, that.Caption) &&
                Objects.equals(Vendor, that.Vendor) &&
                Objects.equals(Version, that.Version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Caption, Vendor, Version);
    }

    public SoftwareListDto(String caption, String version, boolean notShowVendor) {
        Caption = caption;
        Version = version;
        this.notShowVendor = notShowVendor;
    }
}
