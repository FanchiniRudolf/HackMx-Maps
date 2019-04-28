package mx.rjfr.hackmx_maps;

import mx.rjfr.hackmx_maps.Remote.IGoogleApi;
import mx.rjfr.hackmx_maps.Remote.RetrofitClient;

public class Common {
    public static final String baseURL = "http://googleapis.com";
    public static IGoogleApi getGoogleApi(){
        return RetrofitClient.getClient(baseURL).create(IGoogleApi.class);
    }
}
