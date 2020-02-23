package com.niroren.riskengine;

import java.util.Properties;

public interface IRiskEngineService {

    void start(Properties serviceConfig);

    void stop();

}
