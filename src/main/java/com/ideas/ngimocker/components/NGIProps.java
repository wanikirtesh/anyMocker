package com.ideas.ngimocker.components;

import lombok.Getter;
import lombok.Setter;

public class NGIProps {
    @Getter @Setter
    private String host;
    @Getter @Setter
    private String propertyCode;
    @Getter @Setter
    private String clientCode;
    @Getter @Setter
    private String correlationID;
}
