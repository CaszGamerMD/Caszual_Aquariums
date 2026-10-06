package dev.casz.aquarium;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

final class Palette {
 static final List<String> SOILS=new ArrayList<>(List.of("", "sand", "red_sand", "gravel"));
 static final List<String> DECORS=new ArrayList<>(List.of("", "seagrass", "kelp", "sea_pickle", "short_grass", "fern", "dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley", "wither_rose", "stone_button", "polished_blackstone_button"));
 static { for(String color:List.of("white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"))SOILS.add(color+"_concrete_powder");
 for(String wood:List.of("oak","spruce","birch","jungle","acacia","dark_oak","mangrove","cherry","bamboo","pale_oak","crimson","warped"))DECORS.add(wood+"_fence"); }
 static Item item(String name){return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(name));}
 static int find(List<String> list,Item item){for(int i=1;i<list.size();i++)if(item(list.get(i))==item)return i;return 0;}
}