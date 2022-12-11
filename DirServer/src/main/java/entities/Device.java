package entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Setter
@Getter
@Table(name = "Device")
public class Device {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic
    int id;

    @Column
    String UUID;

    @Column
    String name;

    @Column
    String ip;

    @Column
    double hddFreeSpace;

    @Column
    int onlineStatus;
}
