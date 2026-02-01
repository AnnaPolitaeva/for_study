package api.requests.skeleton.interfaces;

import api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(BaseModel model);
    Object update(BaseModel model);
    Object delete(long id);
}
