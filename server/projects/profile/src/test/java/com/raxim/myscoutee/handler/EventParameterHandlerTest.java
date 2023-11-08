package com.raxim.myscoutee.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.raxim.myscoutee.algo.AbstractAlgoTest;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.SettingDTO;
import com.raxim.myscoutee.profile.handler.EventParamHandler;
import com.raxim.myscoutee.profile.service.SettingsService;
import com.raxim.myscoutee.profile.util.AppConstants;

@DirtiesContext
@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = JsonConfig.class)
public class EventParameterHandlerTest extends AbstractAlgoTest {

    private static final UUID UUID_PROFILE_OLIVER = UUID.fromString("534ccc6b-2547-4bf0-ad91-dca739943ea4");

    @InjectMocks
    private EventParamHandler eventParamHandler;

    @Mock
    private SettingsService settingsService;

    @Test
    public void shouldHandleParam() {
        when(settingsService.getSetting(any(), any()))
                                .thenReturn(Optional.of(new SettingDTO()));

        PageParam pageParam = eventParamHandler.handle(new Profile(UUID_PROFILE_OLIVER), new PageParam());
        assertEquals(AppConstants.DAY_FORMAT, pageParam.getGroupKey());
        assertEquals(UUID_PROFILE_OLIVER, pageParam.getId());
        assertEquals(3, pageParam.getOffset().length);
        
        String expectedFrom = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertEquals(expectedFrom, pageParam.getOffset()[0]);

        String expectedCreatedDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertEquals(expectedCreatedDate, pageParam.getOffset()[1]);
    }
}
