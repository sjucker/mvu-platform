/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.pojos;


import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Login implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String name;
    private String password;
    private Boolean active;
    private LocalDateTime lastLogin;
    private String usersPermission;

    public Login() {}

    public Login(Login value) {
        this.id = value.id;
        this.email = value.email;
        this.name = value.name;
        this.password = value.password;
        this.active = value.active;
        this.lastLogin = value.lastLogin;
        this.usersPermission = value.usersPermission;
    }

    public Login(
        Long id,
        String email,
        String name,
        String password,
        Boolean active,
        LocalDateTime lastLogin,
        String usersPermission
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.active = active;
        this.lastLogin = lastLogin;
        this.usersPermission = usersPermission;
    }

    /**
     * Getter for <code>public.login.id</code>.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.login.id</code>.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.login.email</code>.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Setter for <code>public.login.email</code>.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for <code>public.login.name</code>.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>public.login.name</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>public.login.password</code>.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Setter for <code>public.login.password</code>.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter for <code>public.login.active</code>.
     */
    public Boolean getActive() {
        return this.active;
    }

    /**
     * Setter for <code>public.login.active</code>.
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Getter for <code>public.login.last_login</code>.
     */
    public LocalDateTime getLastLogin() {
        return this.lastLogin;
    }

    /**
     * Setter for <code>public.login.last_login</code>.
     */
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Getter for <code>public.login.users_permission</code>.
     */
    public String getUsersPermission() {
        return this.usersPermission;
    }

    /**
     * Setter for <code>public.login.users_permission</code>.
     */
    public void setUsersPermission(String usersPermission) {
        this.usersPermission = usersPermission;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Login other = (Login) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.email == null) {
            if (other.email != null)
                return false;
        }
        else if (!this.email.equals(other.email))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.password == null) {
            if (other.password != null)
                return false;
        }
        else if (!this.password.equals(other.password))
            return false;
        if (this.active == null) {
            if (other.active != null)
                return false;
        }
        else if (!this.active.equals(other.active))
            return false;
        if (this.lastLogin == null) {
            if (other.lastLogin != null)
                return false;
        }
        else if (!this.lastLogin.equals(other.lastLogin))
            return false;
        if (this.usersPermission == null) {
            if (other.usersPermission != null)
                return false;
        }
        else if (!this.usersPermission.equals(other.usersPermission))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.email == null) ? 0 : this.email.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.password == null) ? 0 : this.password.hashCode());
        result = prime * result + ((this.active == null) ? 0 : this.active.hashCode());
        result = prime * result + ((this.lastLogin == null) ? 0 : this.lastLogin.hashCode());
        result = prime * result + ((this.usersPermission == null) ? 0 : this.usersPermission.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Login (");

        sb.append(id);
        sb.append(", ").append(email);
        sb.append(", ").append(name);
        sb.append(", ").append(password);
        sb.append(", ").append(active);
        sb.append(", ").append(lastLogin);
        sb.append(", ").append(usersPermission);

        sb.append(")");
        return sb.toString();
    }
}