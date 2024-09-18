package cz.gennario.gennarioframework.gui.utils;

import cz.gennario.gennarioframework.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InventoryBackgrounding {

    @Data
    @AllArgsConstructor
    public static class BackgroundData {
        private String row1, row2, row3, row4, row5, row6;

        public String getRow(int row) {
            switch (row) {
                case 1:
                    return row1;
                case 2:
                    return row2;
                case 3:
                    return row3;
                case 4:
                    return row4;
                case 5:
                    return row5;
                case 6:
                    return row6;
                default:
                    return "";
            }
        }
    }

    private int rows;
    private Map<Integer, BackgroundData> backgrounds;

    private String preBackground;
    private String title;

    public InventoryBackgrounding(int rows, String preBackground, String title) {
        this.rows = rows;
        this.preBackground = preBackground;
        this.title = title;
        backgrounds = new HashMap<>();
    }

    public void clear() {
        backgrounds.clear();
    }

    public String generateBackground() {
        int maxSlots = rows * 9;

        int totalOffset = 0;
        StringBuilder stringBuilder = new StringBuilder(Utils.colorize(preBackground));
        stringBuilder.append("<shift:13>");

        int currentRow = 0;
        int rowOffset = 0;
        for (int i = 0; i < maxSlots; i++) {
            if(i % 9 == 0) {
                currentRow++;
                stringBuilder.append("<shift:-"+rowOffset+">");
                rowOffset = 0;
            }
            if(!backgrounds.containsKey(i)) {
                stringBuilder.append("<shift:20>");
                rowOffset += 18;
                totalOffset += 18;
            }else {
                BackgroundData backgroundData = backgrounds.get(i);
                stringBuilder.append(backgroundData.getRow(currentRow)).append("<shift:2>");
                rowOffset += 18;
                totalOffset += 18;
            }
        }

        stringBuilder.append("<shift:-"+rowOffset+">").append(Utils.colorize(title));

        return stringBuilder.toString();
    }

    public InventoryBackgrounding append(int slot, BackgroundData background) {
        backgrounds.put(slot, background);
        return this;
    }

    public InventoryBackgrounding append(int row, int slot, BackgroundData background) {
        backgrounds.put((((row-1) * 9) + slot), background);
        return this;
    }


}
