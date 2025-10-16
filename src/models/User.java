package models;

/**
 *
 * @author USER
 */
public class User {
    private int id;
    private String name;
    private String username;
    private String password;

    /**
     *
     * @param id
     * @param name
     * @param username
     * @param password
     */
    public User(int id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    /**
     *
     * @return
     */
    public int getId() { return id; }

    /**
     *
     * @return
     */
    public String getName() { return name; }

    /**
     *
     * @return
     */
    public String getUsername() { return username; }

    /**
     *
     * @return
     */
    public String getPassword() { return password; }

    /**
     *
     * @param id
     */
    public void setId(int id) { this.id = id; }

    /**
     *
     * @param name
     */
    public void setName(String name) { this.name = name; }

    /**
     *
     * @param username
     */
    public void setUsername(String username) { this.username = username; }

    /**
     *
     * @param password
     */
    public void setPassword(String password) { this.password = password; }
}