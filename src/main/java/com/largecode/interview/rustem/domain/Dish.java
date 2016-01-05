/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import javax.persistence.*;

/**
 *
 * Domain object and DTO is the same in this simple project
 * @author Rustem.Zhunusov_at_gmail.com
 */
@Entity
@Table(name = "dish")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Dish", nullable = false, updatable = false)
    @ApiModelProperty(value = "Id of Dish from DB.", required = true)
    private Long idDish;

    @JoinColumn( name = "the_menu", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    LunchMenu theMenu;
    
    @Column(name = "name", nullable = false)
    @ApiModelProperty(value = "Description of Dish.", required = true)
    String name;
    
    @Column(name = "price", nullable = false) // , precision = 20, scale = 4
    @JsonIgnore
    BigDecimal price;

    public Dish(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public Dish() {
    }

    public Long getIdDish() {
        return idDish;
    }

    public void setIdDish(Long idDish) {
        this.idDish = idDish;
    }

    public LunchMenu getTheMenu() {
        return theMenu;
    }

    public void setTheMenu(LunchMenu theMenu) {
        this.theMenu = theMenu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @ApiModelProperty(value = "Price of Dish in string format.", required = true)
    public String getPriceStr() {

        return price.toPlainString();
    }

    @Override
    public String toString() {
        return "Dish{" +
                "idDish=" + idDish +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", theMenu=" + theMenu.getIdLunchMenu() +
                '}';
    }
}
