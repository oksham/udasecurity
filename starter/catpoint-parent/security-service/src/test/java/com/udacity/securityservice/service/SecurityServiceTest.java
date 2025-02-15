package com.udacity.securityservice.service;

import com.udacity.securityservice.service.SecurityService;
import com.udacity.imageservice.service.ImageService;
import com.udacity.securityservice.data.*;
import com.udacity.securityservice.application.StatusListener;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private Sensor sensor1;
    @Mock
    private Sensor sensor2;
    @Mock
    private SecurityRepository securityRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private BufferedImage bufferedImage;
    @Mock
    private Set<Sensor> sensorSet;
    @Mock
    private StatusListener statusListener1;
    @Mock
    private StatusListener statusListener2;
    @InjectMocks
    private SecurityService securityService;

    @BeforeEach
    void initialize() {
        this.securityService = new SecurityService(securityRepository, imageService);
    }


    @Test
    public void shouldAlwaysReturnTrue() {
        assertTrue(true);
    }

    @Test
    public void shouldSetAlarmToPendingWhenSensorActivated() {
        ArmingStatus currentArmingStatus = ArmingStatus.ARMED_HOME;
        AlarmStatus initialAlarmStatus = AlarmStatus.NO_ALARM;
        when(securityRepository.getArmingStatus()).thenReturn(currentArmingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(initialAlarmStatus);
        securityService.activateSensor(sensor1, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    public void shouldSetAlarmToActiveWhenSensorTriggeredInPendingState() {
        ArmingStatus currentArmingStatus = ArmingStatus.ARMED_HOME;
        AlarmStatus pendingStatus = AlarmStatus.PENDING_ALARM;
        when(securityRepository.getArmingStatus()).thenReturn(currentArmingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(pendingStatus);
        securityService.activateSensor(sensor1, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void shouldSetNoAlarmWhenAllSensorsAreInactive() {
        AlarmStatus pendingStatus = AlarmStatus.PENDING_ALARM;
        when(securityRepository.getAlarmStatus()).thenReturn(pendingStatus);
        when(sensor1.getActive()).thenReturn(true);
        securityService.activateSensor(sensor1, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void shouldNotAlterAlarmIfActiveWhenSensorDeactivated() {
        lenient().when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.activateSensor(sensor1, false);
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"DISARMED", "ARMED_HOME", "ARMED_AWAY"})
    public void shouldTriggerAlarmIfCatDetectedInArmedHomeMode() {
        mockArmingStatus(ArmingStatus.ARMED_HOME);
        mockCatPresence(true);
        securityService.processImage(bufferedImage);
        verifyAlarmStatus(AlarmStatus.ALARM);
    }

    private void mockArmingStatus(ArmingStatus status) {
        when(securityRepository.getArmingStatus()).thenReturn(status);
    }

    private void mockCatPresence(boolean isPresent) {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(isPresent);
    }

    private void verifyAlarmStatus(AlarmStatus expectedStatus) {
        verify(securityRepository).setAlarmStatus(expectedStatus);
    }

    @Test
    public void shouldSetAlarmToNoWhenSystemDisarmed() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void shouldDeactivateSensorsWhenSystemArmed() {
        when(securityService.getSensors()).thenReturn(Set.of(sensor1, sensor2));
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(sensor1).setActive(false);
        verify(sensor2).setActive(false);
    }

    @Test
    public void shouldNotTriggerAlarmIfCatDetectedButSystemIsNotArmedHome() {
        ArmingStatus currentArmingStatus = ArmingStatus.ARMED_AWAY;
        boolean catPresent = true;
        when(securityRepository.getArmingStatus()).thenReturn(currentArmingStatus);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(catPresent);
        securityService.processImage(bufferedImage);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"DISARMED", "ARMED_HOME", "ARMED_AWAY"})
    public void shouldSetNoAlarmIfNoCatDetectedAndAllSensorsInactive() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor1, sensor2));
        setSensorInactive(sensor1);
        setSensorInactive(sensor2);
        securityService.processImage(bufferedImage);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    private void setSensorInactive(Sensor sensor) {
        when(sensor.getActive()).thenReturn(false);
    }

    @Test
    public void shouldNotifyListenerOnSensorStatusChange() {
        securityService.addStatusListener(statusListener1);
        securityService.activateSensor(sensor1, true);
        verify(statusListener1).sensorStatusChanged();
    }

}
