package com.predic8.stock.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ObjectError {

    String name;
    String object;
}