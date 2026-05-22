import { useRef, useState } from 'react';
import { uploadFile } from '../lib/api';
import { icon } from '../lib/icons';
import { FileAttachButton } from './FileAttachButton';

const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp', 'application/pdf'];
const MAX_SIZE = 5 * 1024 * 1024;

interface Props {
  value: string;
  onChange: (value: string) => void;
  onAttach: (publicUrl: string, filename: string, attachmentId: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

export function AttachableTextarea({ value, onChange, onAttach, placeholder, disabled }: Props) {
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const dragDepthRef = useRef(0);

  const handleDragEnter = (e: React.DragEvent) => {
    e.preventDefault();
    dragDepthRef.current += 1;
    if (dragDepthRef.current === 1) setIsDragging(true);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    dragDepthRef.current -= 1;
    if (dragDepthRef.current === 0) setIsDragging(false);
  };

  const handleDrop = async (e: React.DragEvent) => {
    e.preventDefault();
    dragDepthRef.current = 0;
    setIsDragging(false);

    const file = e.dataTransfer.files[0];
    if (!file) return;

    if (!ALLOWED_TYPES.includes(file.type)) {
      setUploadError('지원하지 않는 파일 형식입니다. (PNG, JPEG, GIF, WEBP, PDF)');
      return;
    }
    if (file.size > MAX_SIZE) {
      setUploadError('파일 크기는 5MB 이하여야 합니다.');
      return;
    }

    setUploadError(null);
    setIsUploading(true);
    try {
      const { publicUrl, attachmentId } = await uploadFile(file);
      onAttach(publicUrl, file.name, attachmentId);
    } catch (err) {
      setUploadError((err as Error).message ?? '파일 업로드에 실패했습니다.');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div
      className={`textarea-wrap${isDragging ? ' textarea-wrap--dragging' : ''}`}
      onDragEnter={handleDragEnter}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {isDragging && (
        <div className="textarea-wrap__drop-overlay">
          <img src={icon('paperclip')} alt="" width={28} height={28} />
          <span>파일을 여기에 놓으세요</span>
        </div>
      )}
      <textarea
        className="text-area"
        placeholder={placeholder}
        value={value}
        disabled={disabled || isUploading}
        onChange={(e) => onChange(e.target.value)}
      />
      {value.length > 0 && (
        <div className="textarea-wrap__counter">
          띄어쓰기 포함 {value.length}자
        </div>
      )}
      <hr className="textarea-wrap__divider" />
      <FileAttachButton onAttach={onAttach} disabled={disabled || isUploading} />
      {uploadError && <p className="textarea-wrap__attach-error">{uploadError}</p>}
    </div>
  );
}
