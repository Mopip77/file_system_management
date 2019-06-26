package experiment.os;

import experiment.os.user.User;

public class Session {
    private String[] currentPath;
    private User user;

    public Session(String[] currentPath, User user) {
        this.currentPath = currentPath;
        this.user = user;
    }

    public String getCurrentPath() {
        StringBuilder sb = new StringBuilder();
        for (String s : currentPath) {
            sb.append("/" + s);
        }
        return sb.toString();
    }

    public String[] getCurrentPathArray() {
        return currentPath;
    }

    public void setCurrentPath(String[] currentPath) {
        this.currentPath = currentPath;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
