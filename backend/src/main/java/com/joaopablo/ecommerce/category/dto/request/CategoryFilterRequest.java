package com.joaopablo.ecommerce.category.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryFilterRequest {

    private String name;

    private Boolean active;

}