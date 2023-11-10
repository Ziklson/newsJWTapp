package nicetu.kuvarin.news.newsJwtApp.exception;

public class UserNotAuthorizedException extends Exception{
    public UserNotAuthorizedException(String message) {
        super(message);
    }
}
