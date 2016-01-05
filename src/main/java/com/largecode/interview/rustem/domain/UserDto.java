package com.largecode.interview.rustem.domain;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by r.zhunusov on 18.12.2015.
 */
public class UserDto {
    public static final String NO_PASSWORD="";

    @NotNull
    @ApiModelProperty(value = "Id of User inside DB.")
    private Long idUser = DomainUtils.NO_ID;

    @NotEmpty
    @Email
    @ApiModelProperty(value = "Email of User as well as login name.", required = true)
    private String email = "";


    @ApiModelProperty(value = "Password of User or empty if I don't want to change password. No less than 6 char. ", required = true)
    private String password = NO_PASSWORD;


    @ApiModelProperty(value = "Must be equal with Password.", required = true)
    private String passwordRepeated = NO_PASSWORD;

    @NotNull
    @ApiModelProperty(value = "Role of User: 'ADMIN' or 'REGULAR'.", required = true )
    private Role role = Role.REGULAR;

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeated() {
        return passwordRepeated;
    }

    public void setPasswordRepeated(String passwordRepeated) {
        this.passwordRepeated = passwordRepeated;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "idUser=" + idUser +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }


}
