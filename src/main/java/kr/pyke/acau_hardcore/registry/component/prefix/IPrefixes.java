package kr.pyke.acau_hardcore.registry.component.prefix;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.List;

public interface IPrefixes extends ComponentV3, AutoSyncedComponent {
    List<String> getPrefixes();
    String getSelectedPrefix();
    void addPrefix(String id);
    void removePrefix(String id);
    void selectPrefix(String id);
    void clearAll();
}
