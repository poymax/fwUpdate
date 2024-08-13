package switches;

public enum Model {
    S2965 ("SNR-S2965-24T"),
    S2982G_POE ("SNR-S2982G-24T-POE"),
    S2982G_POE_E ("SNR-S2982G-24T-POE-E"),
    S2985G_UPS ("SNR-S2985G-24T-UPS"),
    S2985G_POE ("SNR-S2985G-24T-POE"),
    S2985G_8T_POE ("SNR-S2985G-8T-POE");
    private final String typeName;
    Model(String typeName) {
       this.typeName = typeName;
    }
    public String getTypeName() {
        return typeName;
    }
}
