package com.krikaliov.earthdistances;

public class EarthDataView {
  private final EarthData dataRef;

  public static final float SPACEBOX_COLOR_RED = 0.1f;
  public static final float SPACEBOX_COLOR_BLUE = 0.0f;
  public static final float SPACEBOX_COLOR_GREEN = 0.2f;
  public static final float SPACEBOX_COLOR_ALPHA = 0.0f;

  public EarthDataView(EarthData dataRef) {
    this.dataRef = dataRef;
  }

  @Override
  public String toString() {
    String radiusInMeterStr = Double.toString(EarthData.RADIUS);
    String radiusInMilesStr = Double.toString(this.dataRef.radiusInMiles());

    return "Earth's radius : " + radiusInMeterStr + " km (" + radiusInMilesStr + " miles)";
  }
}
