package com.joaopablo.ecommerce.category.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    private Boolean active;

}