package vultr;

import lombok.Data;

@Data
public class FireWallRule {

    private int rulenumber;
    private String action;
    private String protocol;
    private int port;
    private String subnet;
    private int subnet_size;
    private String notes;

}
