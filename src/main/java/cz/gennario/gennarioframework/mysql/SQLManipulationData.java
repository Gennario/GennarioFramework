package cz.gennario.gennarioframework.mysql;

import lombok.Data;

@Data
public class SQLManipulationData {

    private String column;
    private Object value;

    public SQLManipulationData(String column, Object value) {
        this.column = column;
        this.value = value;
    }

}
