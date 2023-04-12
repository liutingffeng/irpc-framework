package org.example.irpc.framework.interfaces;

import java.util.List;

public interface DataService {

    String sendData(String body);

    List<String> getList();
}
