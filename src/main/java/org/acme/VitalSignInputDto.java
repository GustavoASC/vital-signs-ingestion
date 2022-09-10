package org.acme;

import java.util.Objects;

public class VitalSignInputDto {

    private final String service;
    private final String vitalSign;
    private final Integer userPriority;

    public VitalSignInputDto(String service, String vitalSign, int userPriority) {
        this.service = service;
        this.vitalSign = vitalSign;
        this.userPriority = userPriority;
    }

    public String getService() {
        return this.service;
    }

    public String getVitalSign() {
        return this.vitalSign;
    }

    public int getUserPriority() {
        return this.userPriority;
    }

    @Override
    public String toString() {
        return "{" +
                " service='" + getService() + "'" +
                ", vitalSign='" + getVitalSign() + "'" +
                ", userPriority='" + getUserPriority() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VitalSignInputDto)) {
            return false;
        }
        VitalSignInputDto vitalSignInputDto = (VitalSignInputDto) o;
        return Objects.equals(service, vitalSignInputDto.service)
                && Objects.equals(vitalSign, vitalSignInputDto.vitalSign)
                && userPriority == vitalSignInputDto.userPriority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, vitalSign, userPriority);
    }

}
