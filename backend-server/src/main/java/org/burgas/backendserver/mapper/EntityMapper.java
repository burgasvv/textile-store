package org.burgas.backendserver.mapper;

import org.burgas.backendserver.dto.Request;
import org.burgas.backendserver.dto.Response;
import org.burgas.backendserver.entity.AbstractEntity;
import org.burgas.backendserver.exception.EntityFieldEmptyException;
import org.springframework.stereotype.Component;

@Component
public interface EntityMapper<T extends Request, S extends AbstractEntity, V extends Response> {

    default <D> D handleData(D requestData, D entityData) {
        return requestData == null || requestData == "" ? entityData : requestData;
    }

    default <D> D handleDataThrowable(D requestData, String message) {
        if (requestData == null || requestData == "")
            throw new EntityFieldEmptyException(message);
        return requestData;
    }

    S toEntityMaster(T t);

    S toEntityReplica(T t);

    V toResponse(S s);
}
