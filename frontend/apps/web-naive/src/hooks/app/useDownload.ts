import { message } from '#/adapter/naive';

/**
 * @description 接收数据流生成 blob，创建链接，下载文件
 * @param {Function} api 导出表格的api方法 (必传)
 * @param {string} fileName 导出的文件名 (可选，例如：导出数据.xlsx，默认从响应头或时间戳生成)
 * @param {string} fileType 导出的文件格式 (默认为.xlsx)
 * @param {boolean} isNotify 是否显示导出提示消息 (默认为 false)
 * @returns {Promise<void>} 无返回值
 */
interface NavigatorWithMsSaveOrOpenBlob extends Navigator {
  msSaveOrOpenBlob: (blob: Blob, fileName: string) => void;
}

export const useDownload = async (
  api: () => Promise<any>,
  fallbackFileName = ''
) => {

  try {
    const response = await api();

    const blob = new Blob([response.data]);
    // 1. 校验文件大小（严格满足 size > 0）
    if (blob.size === 0) {
      throw new Error('文件数据为空或生成失败');
    }

    // 2. 提取文件名（优先从 Content-Disposition 获取，支持中文）
    let fileName = fallbackFileName;
    if (!fileName) {
      const disposition = response.headers['content-disposition'];
      if (disposition) {
        // 匹配 filename="xxx.pdf" 或 filename=xxx.pdf
        const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (match?.[1]) {
          fileName = decodeURIComponent(match[1].replace(/['"]/g, ''));
        }
      }
    }

    // 3. 识别后端返回的 JSON 错误（伪装成 blob 返回）
    const contentType = (response.headers['content-type'] || blob.type || '').toLowerCase();
    if (contentType.includes('application/json')) {
      const text = await blob.text();
      try {
        const errorData = JSON.parse(text);
        throw new Error(errorData.message || errorData.msg || errorData.error || '接口返回错误');
      } catch {
        throw new Error('解析错误信息失败');
      }
    }

    // 4. 触发浏览器下载
    if ('msSaveOrOpenBlob' in navigator) { 
      // 兼容 IE/Edge 浏览器
      return (navigator as unknown as NavigatorWithMsSaveOrOpenBlob).msSaveOrOpenBlob(blob, fileName);
    }

    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.style.display = 'none';
    link.download = fileName;
    link.href = downloadUrl;
    document.body.append(link);
    link.click();
    // 延迟清理资源，防止部分浏览器下载中断
    setTimeout(() => {
      document.body.removeChild(link);
      window.URL.revokeObjectURL(downloadUrl);
    }, 100);
  } catch (error) {
    const errorMsg = error instanceof Error ? error.message : '下载失败，请重试';
    message.error(`下载失败: ${errorMsg}`);
    throw error;
  }
};
