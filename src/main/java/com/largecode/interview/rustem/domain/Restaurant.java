/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 *
 * Domain object and DTO is the same in this simple project
 * @author Rustem.Zhunusov_at_gmail.com
 */
@Entity
@Table(name = "restaurant")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_restaurant", nullable = false, updatable = false)
    @ApiModelProperty(value = "Id of Restaurant from DB.", required = true)
    private Long idRestaurant = DomainUtils.NO_ID;

    @Column(name = "id_by_authorities", nullable = false, unique = true)
    @NotEmpty
    @ApiModelProperty(value = "Logical ID of restaurant given by authorities of its country.", required = true)
    private String idByAuthorities="";

    @Column(name = "title", nullable = false)
    @NotEmpty
    @ApiModelProperty(value = "Title of restaurant.", required = true)
    String title="";
    
    @Column(name = "address", nullable = false)
    @NotEmpty
    @ApiModelProperty(value = "Address of restaurant.", required = true)
    String address="";
    
    @Column(name = "phone", nullable = false)
    @ApiModelProperty(value = "Phone number of restaurant.", required = true)
    //TODO: Implement custom validator for phone number
    String phone="";
    
    @Column(name = "lunch_end_hour", nullable = false)
    @Min(value = 11)
    @Max(value = 17)
    @ApiModelProperty(value = "Hour of end of lunch in this restaurant.", required = true)
    Integer lunchEndHour;
    
    @OneToMany(mappedBy = "theRestaurant", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    Set<LunchMenu> theLunchMenus;

    public Long getIdRestaurant() {
        return idRestaurant;
    }

    public void setIdRestaurant(Long idRestaurant) {
        this.idRestaurant = idRestaurant;
    }

    public String getIdByAuthorities() {
        return idByAuthorities;
    }

    public void setIdByAuthorities(String idByAuthorities) {
        this.idByAuthorities = idByAuthorities;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getLunchEndHour() {
        return lunchEndHour;
    }

    public void setLunchEndHour(Integer lunchEndHour) {
        this.lunchEndHour = lunchEndHour;
    }

    public Set<LunchMenu> getTheLunchMenus() {
        return theLunchMenus;
    }

    public void setTheLunchMenus(Set<LunchMenu> theLunchMenus) {
        this.theLunchMenus = theLunchMenus;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "idRestaurant=" + idRestaurant +
                ", idByAuthorities='" + idByAuthorities + '\'' +
                ", title='" + title + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant that = (Restaurant) o;

        if (!idByAuthorities.equals(that.idByAuthorities)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return idByAuthorities.hashCode();
    }
}
