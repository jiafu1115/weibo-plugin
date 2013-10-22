package org.jenkinsci.plugins.weibo;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

public class WeiboAccount implements Serializable{

    private static final long serialVersionUID = 1L;
    private String id;
    private String username;
    private String password;


    /**
     * @param id
     * @param username
     * @param password
     */
    @DataBoundConstructor
    public WeiboAccount(String id, String username, String password) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public WeiboAccount() {
        super();
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }



    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WeiboAccount [id=");
        builder.append(id);
        builder.append(", username=");
        builder.append(username);
        builder.append(", password=");
        builder.append(password);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WeiboAccount other = (WeiboAccount) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
