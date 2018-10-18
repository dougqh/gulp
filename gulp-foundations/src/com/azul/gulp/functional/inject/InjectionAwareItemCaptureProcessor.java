package com.azul.gulp.functional.inject;

import com.azul.gulp.functional.ItemCaptureProcessor;
import com.azul.gulp.inject.InjectionAware;

@Deprecated
public interface InjectionAwareItemCaptureProcessor<T> extends ItemCaptureProcessor<T>, InjectionAware {  
}
