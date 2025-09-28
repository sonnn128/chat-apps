import React, { useState, useRef, useCallback } from 'react';
import PropTypes from 'prop-types';
import { avatarService } from '../services/avatarService';
import { errorToast, successToast } from '../utils/toast';
import './AvatarUpload.css';

const AvatarUpload = ({ 
  userId, 
  currentAvatarUrl, 
  onAvatarChange, 
  size = 'medium',
  showDeleteButton = true,
  disabled = false 
}) => {
  const fileInputRef = useRef(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // Use currentAvatarUrl directly, or preview if available
  const displayUrl = previewUrl || currentAvatarUrl;

  const handleFileSelect = useCallback((file) => {
    if (!file) return;

    // Clear previous error
    setError(null);

    // Validate file
    const validation = avatarService.validateFile(file);
    if (!validation.isValid) {
      const errorMessage = validation.errors.join(', ');
      setError(errorMessage);
      errorToast(errorMessage);
      return;
    }

    // Create preview and store file for later upload
    const preview = avatarService.getFilePreview(file);
    setPreviewUrl(preview);
    setSelectedFile(file);
  }, []);

  const handleSaveAvatar = useCallback(async () => {
    if (!selectedFile || !userId) return;

    setIsLoading(true);
    setError(null);

    try {
      // Upload file
      const result = await avatarService.uploadAvatar(userId, selectedFile);
      
      if (result.success) {
        // Clean up preview and selected file
        avatarService.revokePreview(previewUrl);
        setPreviewUrl(null);
        setSelectedFile(null);
        
        // Notify parent component
        if (onAvatarChange) {
          onAvatarChange(result.data.data?.avatarUrl);
        }
        successToast('Avatar uploaded successfully');
      } else {
        const errorMessage = result.error || 'Failed to upload avatar';
        setError(errorMessage);
        errorToast(errorMessage);
      }
    } catch (error) {
      console.error('Error saving avatar:', error);
      const errorMessage = 'An unexpected error occurred';
      setError(errorMessage);
      errorToast(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }, [selectedFile, userId, previewUrl, onAvatarChange]);

  const handleCancelPreview = useCallback(() => {
    // Clean up preview and selected file
    if (previewUrl) {
      avatarService.revokePreview(previewUrl);
    }
    setPreviewUrl(null);
    setSelectedFile(null);
    setError(null);
  }, [previewUrl]);

  const handleFileInputChange = (event) => {
    const file = event.target.files?.[0];
    if (file) {
      handleFileSelect(file);
    }
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (disabled) return;
    
    const file = e.dataTransfer.files?.[0];
    if (file) {
      handleFileSelect(file);
    }
  };

  const handleClick = () => {
    if (disabled) return;
    fileInputRef.current?.click();
  };

  const handleDelete = async () => {
    if (disabled || !userId) return;
    
    setIsLoading(true);
    setError(null);

    try {
      const result = await avatarService.deleteAvatar(userId);
      if (result.success && onAvatarChange) {
        onAvatarChange(null);
        successToast('Avatar deleted successfully');
      } else {
        const errorMessage = result.error || 'Failed to delete avatar';
        setError(errorMessage);
        errorToast(errorMessage);
      }
    } catch (error) {
      console.error('Error deleting avatar:', error);
      const errorMessage = 'An unexpected error occurred';
      setError(errorMessage);
      errorToast(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const hasPreview = previewUrl && selectedFile;

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="avatar-upload__loading">
          <div className="avatar-upload__spinner"></div>
          <span>Uploading...</span>
        </div>
      );
    }
    
    if (previewUrl) {
      return (
        <img
          src={previewUrl}
          alt="Preview"
          className="avatar-upload__preview"
        />
      );
    }
    
    if (displayUrl) {
      return (
        <img
          src={displayUrl}
          alt="Avatar"
          className="avatar-upload__image"
        />
      );
    }
    
    return (
      <div className="avatar-upload__placeholder">
        <svg className="avatar-upload__icon" viewBox="0 0 24 24">
          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
        </svg>
      </div>
    );
  };

  const getSizeClass = () => {
    switch (size) {
      case 'small': return 'avatar-upload--small';
      case 'large': return 'avatar-upload--large';
      case 'xlarge': return 'avatar-upload--xlarge';
      default: return 'avatar-upload--medium';
    }
  };

  return (
    <div className={`avatar-upload ${getSizeClass()} ${disabled ? 'avatar-upload--disabled' : ''}`}>
      <div
        className={`avatar-upload__container ${dragActive ? 'avatar-upload__container--drag-active' : ''}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        onClick={handleClick}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            handleClick();
          }
        }}
        role="button"
        tabIndex={disabled ? -1 : 0}
        aria-label="Upload avatar"
      >
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handleFileInputChange}
          className="avatar-upload__input"
          disabled={disabled}
        />
        
        <div className="avatar-upload__content">
          {renderContent()}
          
          {!disabled && (
            <div className="avatar-upload__overlay">
              <svg className="avatar-upload__upload-icon" viewBox="0 0 24 24">
                <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
              </svg>
              <span>Upload</span>
            </div>
          )}
        </div>
      </div>
      
      {showDeleteButton && currentAvatarUrl && !hasPreview && !disabled && (
        <button
          type="button"
          className="avatar-upload__delete"
          onClick={handleDelete}
          disabled={isLoading}
        >
          <svg viewBox="0 0 24 24">
            <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z" />
          </svg>
        </button>
      )}

      {/* Preview Actions */}
      {hasPreview && (
        <div className="avatar-upload__preview-actions">
          <button
            type="button"
            className="avatar-upload__btn avatar-upload__btn--save"
            onClick={handleSaveAvatar}
            disabled={isLoading}
          >
            {isLoading ? 'Saving...' : 'Save Avatar'}
          </button>
          <button
            type="button"
            className="avatar-upload__btn avatar-upload__btn--cancel"
            onClick={handleCancelPreview}
            disabled={isLoading}
          >
            Cancel
          </button>
        </div>
      )}
      
      {error && (
        <div className="avatar-upload__error">
          {error}
        </div>
      )}
    </div>
  );
};

AvatarUpload.propTypes = {
  userId: PropTypes.string,
  currentAvatarUrl: PropTypes.string,
  onAvatarChange: PropTypes.func,
  size: PropTypes.oneOf(['small', 'medium', 'large', 'xlarge']),
  showDeleteButton: PropTypes.bool,
  disabled: PropTypes.bool,
};

export default AvatarUpload;
