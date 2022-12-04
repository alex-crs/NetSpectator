package entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class Device {
    int id;
    String UUID;
    String name;
    String ip;
    String tCpu;
    long hddFreeSpace;
}
