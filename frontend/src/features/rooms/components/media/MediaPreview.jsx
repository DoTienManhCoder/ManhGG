export function MediaPreview({ media, controls = false }) {
  if (media.type?.startsWith("video/")) {
    return (
      <video
        className="h-full w-full object-cover"
        src={media.url}
        controls={controls}
        muted={!controls}
        playsInline
        preload="metadata"
      />
    );
  }

  return <img className="h-full w-full object-cover" src={media.url} alt={media.name || "Anh phong"} />;
}
