//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraftforge.eventbus.api;

import java.util.function.Consumer;

public interface IEventBus {
    void register(Object var1);

    <T extends Object> void addListener(Consumer<T> var1);
}
