package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.SystemConfigurationDto;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.repository.SystemConfigurationRepository;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigurationService {

    private final SystemConfigurationRepository repository;

    public void save(SystemConfigurationDto dto) {
        repository.save(SystemConfigurationDto.to(dto));
    }

    public void setConfigurationByName(SystemConfiguration configuration,
                                       String value) {
        try {
            repository.setConfigurationByName(value, configuration.name());
        } catch (Exception ex) {
            throw new EntityNotFoundException("System configuration", configuration.name());
        }
    }

    public String findByName(SystemConfiguration configuration) {
        return repository.findByName(configuration.name()).orElseThrow(() ->
                new EntityNotFoundException("System configuration", configuration.name())).getValue();
    }

    public BigDecimalWrapper findBdByName(SystemConfiguration systemConfiguration) {
        return new BigDecimalWrapper(findByName(systemConfiguration));
    }

    public double findDoubleByName(SystemConfiguration systemConfiguration) {
        return Double.parseDouble(findByName(systemConfiguration));
    }

    public Integer findIntegerByName(SystemConfiguration systemConfiguration) {
        return Integer.valueOf(findByName(systemConfiguration));
    }
}
