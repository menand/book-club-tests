package api;

/**
 * Общий API-клиент — единая точка доступа к клиентам эндпоинтов.
 */
public class ApiClient {

    public final AuthApiClient auth = new AuthApiClient();
    public final UsersApiClient users = new UsersApiClient();
    public final ClubsApiClient clubs = new ClubsApiClient();

}
