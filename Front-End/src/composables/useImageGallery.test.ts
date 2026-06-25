import { describe, it, expect } from 'vitest';
import { defineComponent, h } from 'vue';
import { mount } from '@vue/test-utils';
import { useImageGallery } from './useImageGallery';

describe('useImageGallery', () => {
  const sampleImages = [
    'https://example.com/img1.jpg',
    'https://example.com/img2.jpg',
    'https://example.com/img3.jpg',
    'https://example.com/img4.jpg',
    'https://example.com/img5.jpg',
  ];

  function mountComposable(images: string[]) {
    let result: ReturnType<typeof useImageGallery>;
    const TestComponent = defineComponent({
      setup() {
        result = useImageGallery(images);
        return () => h('div');
      },
    });
    const wrapper = mount(TestComponent);
    return { result: result!, wrapper };
  }

  it('should initialize at index 0', () => {
    const { result } = mountComposable(sampleImages);

    expect(result.currentIndex.value).toBe(0);
    expect(result.currentImage.value).toBe(sampleImages[0]);
    expect(result.totalImages.value).toBe(5);
  });

  it('should navigate to next image', () => {
    const { result } = mountComposable(sampleImages);

    result.next();
    expect(result.currentIndex.value).toBe(1);
    expect(result.currentImage.value).toBe(sampleImages[1]);
  });

  it('should navigate to previous image', () => {
    const { result } = mountComposable(sampleImages);

    result.next();
    result.next();
    result.previous();
    expect(result.currentIndex.value).toBe(1);
    expect(result.currentImage.value).toBe(sampleImages[1]);
  });

  it('should not go below index 0 on previous', () => {
    const { result } = mountComposable(sampleImages);

    result.previous();
    expect(result.currentIndex.value).toBe(0);
  });

  it('should not exceed last index on next', () => {
    const { result } = mountComposable(sampleImages);

    // Navigate to the end
    for (let i = 0; i < 10; i++) {
      result.next();
    }
    expect(result.currentIndex.value).toBe(4);
  });

  it('should go to specific index', () => {
    const { result } = mountComposable(sampleImages);

    result.goTo(3);
    expect(result.currentIndex.value).toBe(3);
    expect(result.currentImage.value).toBe(sampleImages[3]);
  });

  it('should not go to invalid index (negative)', () => {
    const { result } = mountComposable(sampleImages);

    result.goTo(-1);
    expect(result.currentIndex.value).toBe(0);
  });

  it('should not go to invalid index (out of range)', () => {
    const { result } = mountComposable(sampleImages);

    result.goTo(10);
    expect(result.currentIndex.value).toBe(0);
  });

  it('should handle single image array', () => {
    const { result } = mountComposable(['https://example.com/only.jpg']);

    expect(result.totalImages.value).toBe(1);
    expect(result.currentImage.value).toBe('https://example.com/only.jpg');

    result.next();
    expect(result.currentIndex.value).toBe(0);

    result.previous();
    expect(result.currentIndex.value).toBe(0);
  });

  it('should handle empty images array', () => {
    const { result } = mountComposable([]);

    expect(result.totalImages.value).toBe(0);
    expect(result.currentImage.value).toBe('');
  });
});
