export const COLLAPSE_CLASS = 'ui-collapse';
export const COLLAPSE_INNER_CLASS = 'ui-collapse__inner';

export const getCollapseClasses = (isOpen, options = {}) => {
    const { disablePointerWhenClosed = true } = options;
    const classes = [COLLAPSE_CLASS, isOpen ? 'ui-collapse--open' : 'ui-collapse--closed'];
    if (disablePointerWhenClosed) {
        classes.push(isOpen ? 'pointer-events-auto' : 'pointer-events-none');
    }
    return classes;
};
