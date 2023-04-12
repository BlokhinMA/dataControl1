package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSchoolStarter {

    List<Map<String,Object>> data = new ArrayList<>();

    public JavaSchoolStarter() {}

    public List<Map<String,Object>> execute(String request) throws Exception {

        List<Map<String, Object>> data1;

        if (!request.toUpperCase().contains("WHERE")) {
            if (request.toUpperCase().contains("DELETE")) { // удаляем все строки
                data1 = new ArrayList<>(data);
                data.clear();
                return data1;
            }
            if (request.toUpperCase().contains("SELECT")) // выбираем все строки
                return data;
        }

        List<String> listOfParams = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();

        Pattern p = Pattern.compile("'[^'\r\n]*'");
        Matcher m = p.matcher(request.toUpperCase().replaceAll(" ", ""));

        while (m.find()) {
            listOfParams.add(m.group().replaceAll("'", ""));
            numbers.add(m.end());
        }

        for (int i = 0; i < listOfParams.size(); i++) {
            if (!Objects.equals(listOfParams.get(i), "ID") &&
                !Objects.equals(listOfParams.get(i), "LASTNAME") &&
                !Objects.equals(listOfParams.get(i), "AGE") &&
                !Objects.equals(listOfParams.get(i), "COST") &&
                !Objects.equals(listOfParams.get(i), "ACTIVE"))
                throw new Exception("Invalid parameter name");
            if (Objects.equals(listOfParams.get(i).toUpperCase(), "LASTNAME") && !request.replaceAll(" ", "").toUpperCase().substring(numbers.get(i), numbers.get(i) + 5).contains("=NULL"))
                i++;
        }

        Map<String, Object> values = new HashMap<>();

        String[] wholeRequest;
        String[] mainArray;
        String[] optionsArray;

        List<Integer> matchingItemNumbers = new ArrayList<>();

        boolean isThereWhere = request.toUpperCase().contains("WHERE");

        if (isThereWhere) {
            wholeRequest = request.split("(?i)WHERE");
            mainArray = wholeRequest[0].split("'");

            readMain(values, mainArray, true);

            optionsArray = wholeRequest[1].split("'");

            List<Object []> params = new ArrayList<>();
            Object [] param;
            List<String> logicalOperators = new ArrayList<>();
            List<Integer> forLogicalOperators = new ArrayList<>();

            for (int i = 0; i < optionsArray.length; i++) {
                switch (optionsArray[i].trim().toUpperCase()) {
                    case "ID" -> {
                        isComparisonOperatorCompatibility(comparisonOperator(optionsArray[i + 1]));
                        param = new Object[3];
                        param[0] = "id";
                        param[1] = comparisonOperator(optionsArray[i + 1]);
                        if (optionsArray[i + 1].toUpperCase().contains("NULL"))
                            param[2] = null;
                        else param[2] = Long.parseLong(toDigits(optionsArray[i + 1], false));
                        params.add(param);
                        forLogicalOperators.add(i + 1);
                    }
                    case "LASTNAME" -> {
                        if (!(comparisonOperator(optionsArray[i + 1]).equals("like") ||
                              comparisonOperator(optionsArray[i + 1]).equals("ilike") ||
                              comparisonOperator(optionsArray[i + 1]).equals("=") ||
                              comparisonOperator(optionsArray[i + 1]).equals("!=")))
                            throw new Exception("Invalid comparison operator");
                        param = new Object[3];
                        param[0] = "lastName";
                        param[1] = comparisonOperator(optionsArray[i + 1]);
                        if (optionsArray[i + 1].toUpperCase().contains("NULL")) {
                            param[2] = null;
                            forLogicalOperators.add(i + 1);
                        }
                        else {
                            param[2] = optionsArray[i + 2];
                            forLogicalOperators.add(i + 3);
                        }
                        params.add(param);
                    }
                    case "AGE" -> {
                        isComparisonOperatorCompatibility(comparisonOperator(optionsArray[i + 1]));
                        param = new Object[3];
                        param[0] = "age";
                        param[1] = comparisonOperator(optionsArray[i + 1]);
                        if (optionsArray[i + 1].toUpperCase().contains("NULL"))
                            param[2] = null;
                        else param[2] = Long.parseLong(toDigits(optionsArray[i + 1], false));
                        params.add(param);
                        forLogicalOperators.add(i + 1);
                    }
                    case "COST" -> {
                        isComparisonOperatorCompatibility(comparisonOperator(optionsArray[i + 1]));
                        param = new Object[3];
                        param[0] = "cost";
                        param[1] = comparisonOperator(optionsArray[i + 1]);
                        if (optionsArray[i + 1].toUpperCase().contains("NULL"))
                            param[2] = null;
                        else param[2] = Double.parseDouble(toDigits(optionsArray[i + 1], false));
                        params.add(param);
                        forLogicalOperators.add(i + 1);
                    }
                    case "ACTIVE" -> {
                        if (!(comparisonOperator(optionsArray[i + 1]).equals("=") ||
                              comparisonOperator(optionsArray[i + 1]).equals("!=")))
                            throw new Exception("Invalid comparison operator");
                        param = new Object[3];
                        if (!optionsArray[i + 1].toUpperCase().contains("NULL"))
                            if (optionsArray[i + 1].toLowerCase().matches("\s*[=!]{1,2}\s*true\s+.*"))
                                param[2] = true;
                            else if (optionsArray[i + 1].toLowerCase().matches("\s*[=!]{1,2}\s*false\s+.*"))
                                param[2] = false;
                            else throw new Exception("Invalid parameter value");
                        else param[2] = null;
                        param[0] = "active";
                        param[1] = comparisonOperator(optionsArray[i + 1]);
                        params.add(param);
                        forLogicalOperators.add(i + 1);
                    }
                    default -> {
                    }
                }
            }

            for (int i = 0; i < forLogicalOperators.size() - 1; i++) {
                if (optionsArray[forLogicalOperators.get(i)].toUpperCase().matches(".*\s+AND\s*"))
                    logicalOperators.add("AND");
                else if (optionsArray[forLogicalOperators.get(i)].toUpperCase().matches(".*\s+OR\s*"))
                    logicalOperators.add("OR");
                else throw new Exception("Unsupported logical operator");
            }

            if (params.size() >= 2) {

                List<Boolean> list;

                for (int i = 0; i < data.size(); i++) {
                    list = new ArrayList<>();
                    for (int j = 0; j < params.size(); j++) {
                        if (j == params.size() - 1) {
                            if (Objects.equals(logicalOperators.get(j - 1), "AND"))
                                if (Objects.equals(logicalOperators.get(j - 2), "AND")) {
                                    list.add(list.get(list.size() - 1) && isParamTrue(data.get(i), params.get(j))); // если предыдущий логический оператор - AND,
                                    // то последнее значение в списке булевых переменных умножается на значение текущего параметра и предпоследнее значение в списке удаляется
                                    list.remove(list.size() - 2);
                                } else list.add(isParamTrue(data.get(i), params.get(j - 1)) && isParamTrue(data.get(i), params.get(j))); // умножает значение параметра, стоящего перед "AND", на значение параметра, стоящего после "AND"
                            else list.add(isParamTrue(data.get(i), params.get(j)));
                            break;
                        }

                        if (Objects.equals(logicalOperators.get(j), "AND"))
                            if (j > 0 && Objects.equals(logicalOperators.get(j - 1), "AND")) {
                                list.add(list.get(list.size() - 1) && isParamTrue(data.get(i), params.get(j))); // если предыдущий логический оператор - AND,
                                // то последнее значение в списке булевых переменных умножается на значение текущего параметра и предпоследнее значение в списке удаляется
                                list.remove(list.size() - 2);
                            } else {
                                list.add(isParamTrue(data.get(i), params.get(j)) && isParamTrue(data.get(i), params.get(j + 1))); // умножает значение параметра, стоящего перед "AND", на значение параметра, стоящего после "AND"
                                j++;
                            }
                        else list.add(isParamTrue(data.get(i), params.get(j)));
                    }

                    boolean check;

                    for (Boolean aBoolean : list) {
                        check = aBoolean;
                        if (check) {
                            matchingItemNumbers.add(i);
                            break;
                        }
                    }

                }

            } else
                for (int i = 0; i < data.size(); i++)
                    if (isParamTrue(data.get(i), params.get(0)))
                        matchingItemNumbers.add(i);

        } else {
            wholeRequest = request.split("'");

            readMain(values, wholeRequest, false);
        }

        if (request.toUpperCase().contains("INSERT")) {

            if (!request.toUpperCase().contains("VALUES"))
                throw new Exception("The request must contain \"VALUES\"");

            data.add(values);

            data1 = new ArrayList<>();
            data1.add(values);

            return data1;

        } else if (request.toUpperCase().contains("UPDATE")) {

            if (!request.toUpperCase().contains("VALUES"))
                throw new Exception("The request must contain \"VALUES\"");

            if (isThereWhere) {

                data1 = new ArrayList<>();

                for (Integer matchingItemNumber : matchingItemNumbers) { // изменяем строки, удовлетворяющие условию

                    for (Map.Entry<String, Object> value : values.entrySet()) {
                        if (value.getValue() == null)
                            data.get(matchingItemNumber).remove(value.getKey());
                        else data.get(matchingItemNumber).put(value.getKey(), value.getValue());
                    }

                    data1.add(data.get(matchingItemNumber));

                }

                return data1;

            } else {

                for (Map<String, Object> datum : data) { // изменяем все строки

                    for (Map.Entry<String, Object> value : values.entrySet()) {
                        if (value.getValue() == null)
                            datum.remove(value.getKey());
                        else datum.put(value.getKey(), value.getValue());
                    }

                }

                return data;
            }

        } else if (request.toUpperCase().contains("DELETE")) {

            data1 = new ArrayList<>();

            for (int i = matchingItemNumbers.size() - 1; i >= 0; i--) { // удаляем строки, удовлетворяющие условию
                data1.add(data.get(matchingItemNumbers.get(i)));
                data.remove((int) matchingItemNumbers.get(i));
            }

            Collections.reverse(data1);

            return data1;

        } else if (request.toUpperCase().contains("SELECT")) {

            data1 = new ArrayList<>();

            for (Integer matchingItemNumber : matchingItemNumbers) data1.add(data.get(matchingItemNumber)); // выбираем строки, удовлетворяющие условию

            return data1;

        } else throw new Exception("Unsupported operation");

    }

    private boolean isParamTrue(Map<String, Object> data, Object [] param) throws Exception {

        if (Objects.equals(param[1], "=")) {
            if (data.get(param[0].toString()) != null && param[2] instanceof String && data.get(param[0].toString()).equals(param[2]))
                return true;
            return data.get(param[0].toString()) == param[2];
        }
        if (Objects.equals(param[1], "!=")) {
            if (data.get(param[0].toString()) != null && param[2] instanceof String && !data.get(param[0].toString()).equals(param[2]))
                return true;
            return data.get(param[0].toString()) != param[2];
        }
        if (data.get(param[0].toString()) != null) {
            if (Objects.equals(param[1], ">=")) {
                if (param[2] instanceof Long && (Long) data.get(param[0].toString()) >= (Long) param[2])
                    return true;
                return param[2] instanceof Double && (Double) data.get(param[0].toString()) >= (Double) param[2];
            }
            if (Objects.equals(param[1], "<=")) {
                if (param[2] instanceof Long && (Long) data.get(param[0].toString()) <= (Long) param[2])
                    return true;
                return param[2] instanceof Double && (Double) data.get(param[0].toString()) <= (Double) param[2];
            }
            if (Objects.equals(param[1], ">")) {
                if (param[2] instanceof Long && (Long) data.get(param[0].toString()) > (Long) param[2])
                    return true;
                return param[2] instanceof Double && (Double) data.get(param[0].toString()) > (Double) param[2];
            }
            if (Objects.equals(param[1], "<")) {
                if (param[2] instanceof Long && (Long) data.get(param[0].toString()) < (Long) param[2])
                    return true;
                return param[2] instanceof Double && (Double) data.get(param[0].toString()) < (Double) param[2];
            }
            if (Objects.equals(param[1], "like")) {
                if (data.get(param[0].toString()).equals(param[2]))
                    return true;
                if (param[2].toString().startsWith("%") && data.get(param[0].toString()).toString().matches(".*" + param[2].toString().replace("%", "")))
                    return true;
                if (param[2].toString().endsWith("%") && data.get(param[0].toString()).toString().matches(param[2].toString().replace("%", "") + ".*"))
                    return true;
                return param[2].toString().startsWith("%") && param[2].toString().endsWith("%") && data.get(param[0].toString()).toString().matches(".*" + param[2].toString().replace("%", "") + ".*");
            }
            if (Objects.equals(param[1], "ilike")) {
                if (data.get(param[0].toString()).equals(param[2]))
                    return true;
                if (param[2].toString().startsWith("%") && data.get(param[0].toString()).toString().toUpperCase().matches(param[2].toString().replace("%", "").toUpperCase() + ".*"))
                    return true;
                if (param[2].toString().endsWith("%") && data.get(param[0].toString()).toString().matches(".*" + param[2].toString().replace("%", "").toUpperCase()))
                    return true;
                return param[2].toString().startsWith("%") && param[2].toString().endsWith("%") && data.get(param[0].toString()).toString().matches(".*" + param[2].toString().replace("%", "").toUpperCase() + ".*");
            }
        } else if (param[2] == null)
            throw new Exception("Value cannot be compared to null");

        return false;
    }

    private void isComparisonOperatorCompatibility(String comparisonOperator) throws Exception {
        if (!(comparisonOperator.equals("=") ||
              comparisonOperator.equals("!=") ||
              comparisonOperator.equals(">=") ||
              comparisonOperator.equals("<=") ||
              comparisonOperator.equals(">") ||
              comparisonOperator.equals("<")))
            throw new Exception("Invalid comparison operator");
    }

    private String comparisonOperator(String str) throws Exception {

        if (str.toLowerCase().matches("\s*like\s*.*")) {
            str = "like";
            return str;
        }
        if (str.toLowerCase().matches("\s*ilike\s*.*")) {
            str = "ilike";
            return str;
        }
        if (str.matches("\s*=\s*.*")) {
            str = "=";
            return str;
        }
        if (str.matches("\s*!=\s*.*")) {
            str = "!=";
            return str;
        }
        if (str.matches("\s*>=\s*.*")) {
            str = ">=";
            return str;
        }
        if (str.matches("\s*<=\s*.*")) {
            str = "<=";
            return str;
        }
        if (str.matches("\s*>\s*.*")) {
            str = ">";
            return str;
        }
        if (str.matches("\s*<\s*.*")) {
            str = "<";
            return str;
        }

        throw new Exception("Unsupported comparison operator");
    }

    private String toDigits(String str, boolean forMain) throws Exception {
        if (forMain && !str.matches("\s*=\s*\\d+\\.?\\d*\s*,?\s*"))
            throw new Exception("Invalid parameter value or missing \"=\"");
        if (!forMain && !str.matches("\s*.*\s*\\d+\\.?\\d*.*"))
            throw new Exception("Invalid parameter value");
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) >= 48 && str.charAt(i) <= 57 || str.charAt(i) == 46)
                strBuilder.append(str.charAt(i));
        return strBuilder.toString();
    }

    private void readMain(Map<String, Object> values, String[] mainArray, boolean isThereWhere) throws Exception {
        for (int i = 0; i < mainArray.length; i++) {
            switch (mainArray[i].trim().toUpperCase()) {
                case "ID" -> {
                    if (isThereWhere && mainArray[i + 1].toUpperCase().contains("NULL")) {
                        values.put("id", null);
                    } else values.put("id", Long.parseLong(toDigits(mainArray[i + 1], true)));
                }
                case "LASTNAME" -> {
                    if (isThereWhere && mainArray[i + 1].toUpperCase().contains("NULL")) {
                        if (!mainArray[i + 1].matches("\s*=.*"))
                            throw new Exception("Missing \"=\"");
                        values.put("lastName", null);
                    } else
                        if (!mainArray[i + 1].matches("\s*=\s*"))
                            throw new Exception("Missing \"=\"");
                        else values.put("lastName", mainArray[i + 2]);
                }
                case "AGE" -> {
                    if (isThereWhere && mainArray[i + 1].toUpperCase().contains("NULL")) {
                        values.put("age", null);
                    } else values.put("age", Long.parseLong(toDigits(mainArray[i + 1], true)));
                }
                case "COST" -> {
                    if (isThereWhere && mainArray[i + 1].toUpperCase().contains("NULL")) {
                        values.put("cost", null);
                    } else values.put("cost", Double.parseDouble(toDigits(mainArray[i + 1], true)));
                }
                case "ACTIVE" -> {
                    if (!(isThereWhere && mainArray[i + 1].toUpperCase().contains("NULL"))) {
                        if (mainArray[i + 1].toLowerCase().matches("\s*=\s*true\s*,?\s*"))
                            values.put("active", true);
                        else if (mainArray[i + 1].toLowerCase().matches("\s*=\s*false\s*,?\s*"))
                            values.put("active", false);
                        else throw new Exception("Invalid parameter value or missing \"=\"");
                    } else values.put("active", null);
                }
                default -> {
                }
            }
        }
    }

}
