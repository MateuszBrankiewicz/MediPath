import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';
import { CardModule } from 'primeng/card';

export interface CommentItem {
  id: string | number;
  content: string;
}

@Component({
  selector: 'app-comments-card',
  imports: [CommonModule, CardModule],
  template: `
    <p-card class="comments-card">
      <div class="comments-header">
        <i class="pi pi-comments"></i>
        <h3 class="comments-title">{{ title() }}</h3>
      </div>
      <div class="comments-list">
        @for (comment of comments(); track comment.id) {
          <div class="comment-item">
            <i class="pi pi-user"></i>
            <span class="comment-content">{{ comment.content }}</span>
          </div>
        }
      </div>
    </p-card>
  `,
  styles: [
    `
      .comments-card {
        height: 215px;
      }
      .comments-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
      }
      .comments-title {
        margin: 0;
        font-size: 16px;
      }
      .comments-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .comment-item {
        display: flex;
        align-items: center;
        gap: 8px;
        background: var(--p-surface-50);
        padding: 10px 12px;
        border-radius: 12px;
      }
      .comment-content {
        font-size: 14px;
      }
    `,
  ],
})
export class CommentsCard {
  readonly title = input<string>('Comments');
  readonly comments = input<CommentItem[]>([]);
}
