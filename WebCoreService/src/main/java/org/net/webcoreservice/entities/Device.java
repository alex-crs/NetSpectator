package org.net.webcoreservice.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
@NoArgsConstructor
@Entity
@Table(name = "Device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column
    private String UUID;

    @Column
    private String title;

    @Column
    private String ip;

    @Column
    private double hddFreeSpace;

    @Column
    private int onlineStatus;

    @ManyToOne
    @JoinColumn(name="deviceGroup")
    private DeviceGroup deviceGroup;
}
