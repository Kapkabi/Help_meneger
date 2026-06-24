from pathlib import Path
from typing import Optional

from PySide6.QtCore import QSize, Qt
from PySide6.QtGui import QPixmap
from PySide6.QtWidgets import QHBoxLayout, QLabel, QVBoxLayout, QWidget

THUMB_SIZE = 56


class RecipeListItemWidget(QWidget):
    """Row widget for the recipe list: thumbnail + title + category/time."""

    def __init__(self, title: str, category: str, photo_path: Optional[str], cook_time: int, parent=None):
        super().__init__(parent)
        layout = QHBoxLayout(self)
        layout.setContentsMargins(6, 4, 6, 4)

        thumb = QLabel()
        thumb.setFixedSize(THUMB_SIZE, THUMB_SIZE)
        thumb.setAlignment(Qt.AlignCenter)
        thumb.setStyleSheet("background-color: #eee; border-radius: 4px;")
        pixmap = QPixmap(photo_path) if photo_path and Path(photo_path).exists() else None
        if pixmap and not pixmap.isNull():
            thumb.setPixmap(
                pixmap.scaled(THUMB_SIZE, THUMB_SIZE, Qt.KeepAspectRatioByExpanding, Qt.SmoothTransformation)
            )
        else:
            thumb.setText("🍽")
        layout.addWidget(thumb)

        text_layout = QVBoxLayout()
        text_layout.addWidget(QLabel(f"<b>{title}</b>"))
        meta_label = QLabel(f"{category or 'Без категории'} • {cook_time} мин")
        meta_label.setStyleSheet("color: gray;")
        text_layout.addWidget(meta_label)
        layout.addLayout(text_layout)
        layout.addStretch()

    def sizeHint(self):
        return QSize(220, THUMB_SIZE + 8)
