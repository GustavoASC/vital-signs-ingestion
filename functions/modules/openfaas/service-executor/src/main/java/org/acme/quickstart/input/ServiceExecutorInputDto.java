package org.acme.quickstart.input;

import java.util.Objects;
import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ServiceExecutorInputDto {

    private final String serviceName;
    private final String vitalSign;
    private final Integer userPriority;
    private final UUID id;

    public ServiceExecutorInputDto(String serviceName, String vitalSign, int userPriority, UUID id) {
        this.serviceName = serviceName;
        this.vitalSign = vitalSign;
        this.userPriority = userPriority;
        this.id = id;
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

    public UUID getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "{" +
                " serviceName='" + getServiceName() + "'" +
                ", vitalSign='" + getVitalSign() + "'" +
                ", userPriority='" + getUserPriority() + "'" +
                ", id='" + getId() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ServiceExecutorInputDto)) {
            return false;
        }
        ServiceExecutorInputDto serviceExecutorInputDto = (ServiceExecutorInputDto) o;
        return Objects.equals(serviceName, serviceExecutorInputDto.serviceName)
                && Objects.equals(vitalSign, serviceExecutorInputDto.vitalSign)
                && Objects.equals(userPriority, serviceExecutorInputDto.userPriority)
                && Objects.equals(id, serviceExecutorInputDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, vitalSign, userPriority, id);
    }

}
