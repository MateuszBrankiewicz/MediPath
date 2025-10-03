import { computed, Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SpinnerService {
  private loadingCounter = signal(0);
  private operatorCounter = signal(0);

  public readonly isLoading = computed(() => this.loadingCounter() > 0);
  public readonly isOperatorLoading = computed(
    () => this.operatorCounter() > 0,
  );

  public show(): void {
    this.loadingCounter.update((count) => count + 1);
  }

  public hide(): void {
    this.loadingCounter.update((count) => Math.max(0, count - 1));
  }

  public reset(): void {
    this.loadingCounter.set(0);
    this.operatorCounter.set(0);
  }
  public showOperator(): void {
    this.operatorCounter.update((count) => count + 1);
  }

  public hideOperator(): void {
    this.operatorCounter.update((count) => Math.max(0, count - 1));
  }
}
