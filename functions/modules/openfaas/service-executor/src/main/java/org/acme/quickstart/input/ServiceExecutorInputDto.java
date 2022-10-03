package org.acme.quickstart.input;

import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ServiceExecutorInputDto {

    private final String serviceName;
    private final String vitalSign;
    private final Integer userPriority;

    public ServiceExecutorInputDto(String serviceName, String vitalSign, int userPriority) {
        this.serviceName = serviceName;
        this.vitalSign = vitalSign;
        this.userPriority = userPriority;
    }

    public String getServiceName() {
        return this.serviceName;
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
                " serviceName='" + getServiceName() + "'" +
                ", vitalSign='" + getVitalSign() + "'" +
                ", userPriority='" + getUserPriority() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ServiceExecutorInputDto)) {
            return false;
        }
        ServiceExecutorInputDto vitalSignInputDto = (ServiceExecutorInputDto) o;
        return Objects.equals(serviceName, vitalSignInputDto.serviceName)
                && Objects.equals(vitalSign, vitalSignInputDto.vitalSign)
                && userPriority == vitalSignInputDto.userPriority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, vitalSign, userPriority);
    }

}
