package entities;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;


@Data
@NoArgsConstructor
@Entity
@Table(name = "Device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Basic
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
