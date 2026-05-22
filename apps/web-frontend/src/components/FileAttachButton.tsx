import { useRef, useState } from 'react';
import { uploadFile } from '../lib/api';
import { icon } from '../lib/icons';

const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp', 'application/pdf'];
const MAX_SIZE = 5 * 1024 * 1024;

interface Props {
  onAttach: (publicUrl: string, filename: string, attachmentId: string) => void;
  disabled?: boolean;
}

export function FileAttachButton({ onAttach, disabled }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleClick = () => {
    if (disabled || isUploading) return;
    inputRef.current?.click();
  };

  const handleChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;

    if (!ALLOWED_TYPES.includes(file.type)) {
      setError('지원하지 않는 파일 형식입니다. (PNG, JPEG, GIF, WEBP, PDF)');
      return;
    }
    if (file.size > MAX_SIZE) {
      setError('파일 크기는 5MB 이하여야 합니다.');
      return;
    }

    setError(null);
    setIsUploading(true);
    try {
      const { publicUrl, attachmentId } = await uploadFile(file);
      onAttach(publicUrl, file.name, attachmentId);
    } catch (err) {
      setError((err as Error).message ?? '파일 업로드에 실패했습니다.');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <>
      <input
        ref={inputRef}
        type="file"
        accept={ALLOWED_TYPES.join(',')}
        style={{ display: 'none' }}
        onChange={handleChange}
      />
      <button
        type="button"
        className="textarea-wrap__attach"
        disabled={disabled || isUploading}
        onClick={handleClick}
      >
        <img src={icon('paperclip')} alt="" width={16} height={16} />
        {isUploading ? '업로드 중…' : '파일 첨부하기'}
      </button>
      {error && <p className="textarea-wrap__attach-error">{error}</p>}
    </>
  );
}
