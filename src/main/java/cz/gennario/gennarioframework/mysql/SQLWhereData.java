package cz.gennario.gennarioframework.mysql;

import lombok.Data;

@Data
public class SQLWhereData {

    public enum WhereOperator {
        EQUALS("="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_OR_EQUALS(">="),
        LESS_THAN_OR_EQUALS("<="),
        LIKE("LIKE"),
        NOT_LIKE("NOT LIKE"),
        IN("IN"),
        NOT_IN("NOT IN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL");

        private final String operator;

        WhereOperator(String operator) {
            this.operator = operator;
        }

        @Override
        public String toString() {
            return operator;
        }

        public String getOperator() {
            return operator;
        }
    }

    private String column;
    private WhereOperator operator;
    private String value;

    public SQLWhereData(String column, WhereOperator operator, String value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

}
